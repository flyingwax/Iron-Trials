#!/usr/bin/env python3
"""
Sample server endpoint for Iron Trials milestone configurations
This demonstrates how your backend server could serve group-specific milestone configs
"""

from flask import Flask, request, jsonify
import json

app = Flask(__name__)

# Sample milestone configurations for different groups
GROUP_CONFIGS = {
    "test-group": {
        "levelMilestones": [30, 50, 70, 80, 90, 99],
        "questMilestones": [
            "Quest Point Cape",
            "Achievement Diary Cape", 
            "Fire Cape",
            "Infernal Cape",
            "Max Cape",
            "Dragon Slayer",
            "Recipe for Disaster"
        ],
        "achievementMilestones": [
            "Achievement Diary",
            "Diary Cape",
            "Max Cape",
            "Combat Achievements"
        ],
        "rareDrops": [
            "bandos", "armadyl", "saradomin", "zamorak", "guthix",
            "abyssal", "dragon warhammer", "twisted bow", "scythe", "rapier", "blade",
            "dragon axe", "dragon pickaxe", "dragon harpoon",
            "pet", "baby"
        ],
        "bossKills": [
            "Kraken", "Zulrah", "Vorkath", "Hydra", "Gauntlet", "Corrupted Gauntlet"
        ],
        "customMilestones": {
            "First Fire Cape": 100,
            "Quest Cape": 200,
            "Max Cape": 500
        }
    },
    
    "hardcore-group": {
        "levelMilestones": [50, 70, 80, 90, 99],
        "questMilestones": [
            "Quest Point Cape",
            "Fire Cape",
            "Infernal Cape",
            "Max Cape"
        ],
        "achievementMilestones": [
            "Achievement Diary",
            "Diary Cape",
            "Max Cape"
        ],
        "rareDrops": [
            "bandos", "armadyl", "saradomin", "zamorak", "guthix",
            "abyssal", "dragon warhammer", "twisted bow", "scythe",
            "pet", "baby"
        ],
        "bossKills": [
            "Kraken", "Zulrah", "Vorkath", "Hydra"
        ],
        "customMilestones": {
            "First Fire Cape": 150,
            "Quest Cape": 300,
            "Max Cape": 750,
            "Survive 1000 Total Level": 200
        }
    },
    
    "casual-group": {
        "levelMilestones": [20, 40, 60, 80, 99],
        "questMilestones": [
            "Dragon Slayer",
            "Recipe for Disaster",
            "Monkey Madness",
            "Quest Point Cape"
        ],
        "achievementMilestones": [
            "Achievement Diary",
            "Diary Cape"
        ],
        "rareDrops": [
            "dragon", "abyssal", "bandos", "armadyl",
            "pet", "baby"
        ],
        "bossKills": [
            "Kraken", "Zulrah", "Vorkath"
        ],
        "customMilestones": {
            "First Dragon Drop": 50,
            "Quest Cape": 150,
            "Max Cape": 400
        }
    }
}

@app.route('/api/iron-trials/milestones', methods=['GET'])
def get_milestones():
    """Get milestone configuration for a specific group"""
    group_id = request.args.get('groupId', 'test-group')
    
    if group_id not in GROUP_CONFIGS:
        return jsonify({
            "error": f"Group '{group_id}' not found",
            "available_groups": list(GROUP_CONFIGS.keys())
        }), 404
    
    config = GROUP_CONFIGS[group_id]
    
    # Add metadata
    response = {
        "groupId": group_id,
        "version": "1.0",
        "lastUpdated": "2025-01-02T00:00:00Z",
        "config": config
    }
    
    return jsonify(response)

@app.route('/api/iron-trials/groups', methods=['GET'])
def list_groups():
    """List all available groups"""
    return jsonify({
        "groups": list(GROUP_CONFIGS.keys()),
        "count": len(GROUP_CONFIGS)
    })

@app.route('/api/iron-trials/milestones/<group_id>', methods=['PUT'])
def update_milestones(group_id):
    """Update milestone configuration for a group (admin only)"""
    # In a real implementation, you'd add authentication here
    data = request.get_json()
    
    if not data:
        return jsonify({"error": "No data provided"}), 400
    
    # Validate the configuration structure
    required_fields = ["levelMilestones", "questMilestones", "achievementMilestones", "rareDrops", "bossKills", "customMilestones"]
    for field in required_fields:
        if field not in data:
            return jsonify({"error": f"Missing required field: {field}"}), 400
    
    # Update the configuration
    GROUP_CONFIGS[group_id] = data
    
    return jsonify({
        "message": f"Configuration updated for group '{group_id}'",
        "groupId": group_id
    })

if __name__ == '__main__':
    print("Iron Trials Milestone Server")
    print("Available groups:", list(GROUP_CONFIGS.keys()))
    print("Sample URLs:")
    print("  GET /api/iron-trials/milestones?groupId=test-group")
    print("  GET /api/iron-trials/milestones?groupId=hardcore-group")
    print("  GET /api/iron-trials/milestones?groupId=casual-group")
    print("\nStarting server on http://localhost:5000")
    
    app.run(debug=True, host='0.0.0.0', port=5000) 